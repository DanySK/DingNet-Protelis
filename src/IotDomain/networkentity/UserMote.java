package IotDomain.networkentity;

import IotDomain.Environment;
import IotDomain.lora.BasicFrameHeader;
import IotDomain.lora.LoraWanPacket;
import IotDomain.lora.MacCommand;
import IotDomain.lora.MessageType;
import SensorDataGenerators.SensorDataGenerator;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.Path;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Map;

public class UserMote extends Mote {

    private boolean isActive = false;
    private final GeoPosition destination = new GeoPosition(1,1);
    private final LocalTime whenAskPath = LocalTime.of(0, 15);
    private boolean alreadyRequested = false;

    public UserMote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, Path path, Double movementSpeed, Integer startMovementOffset, int periodSendingPacket, int startSendingOffset) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
    }

    public UserMote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, Path path, Double movementSpeed) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed);
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        if (isActive() && !alreadyRequested && whenAskPath.isBefore(getEnvironment().getClock().getTime())) {
            alreadyRequested = true;
            byte[] payload= new byte[17];
            payload[0] = MessageType.REQUEST_PATH.getCode();
            System.arraycopy(getGPSSensor().generateData(getPos(), getEnvironment().getClock().getTime()), 0, payload, 1, 8);
            System.arraycopy(Converter.toByteArray(destination), 0, payload, 9, 8);
            return new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
                new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
        }
        return LoraWanPacket.createEmptyPacket(getEUI(), getApplicationEUI());
    }

    //not used yet
    private void askNewPartOfPath() {
        if (getPath().getDestination().isEmpty()) {
            throw new IllegalStateException("You can't require new part of path without a previous one");
        }
        byte[] payload= new byte[9];
        payload[0] = MessageType.REQUEST_UPDATE_PATH.getCode();
        System.arraycopy(Converter.toByteArray(getPath().getDestination().get()), 0, payload, 1, 8);
        loraSend(new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload),
            new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>()));

        var clock = getEnvironment().getClock();
        var oldPath = getPath();
        clock.addTrigger(clock.getTime().plusSeconds(30), () -> {
            if (oldPath.equals(getPath())) {
                askNewPartOfPath();
            }
            return LocalTime.of(0, 0);
        });
    }

    private SensorDataGenerator getGPSSensor() {
        return getSensors().stream().filter(s -> s.equals(MoteSensor.GPS)).findFirst().orElseThrow().getSensorDataGenerator();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (active) {
            getEnvironment().getMotes().stream()
                .filter(m -> m instanceof UserMote)
                .map(m -> (UserMote)m)
                .forEach(m -> {
                    m.setActive(false);
                    m.enable(false);
                });
        }
        isActive = active;
    }

    @Override
    public Boolean isEnabled() {
        return super.isEnabled() && isActive();
    }

    @Override
    public boolean isArrivedToDestination() {
        var dest = getPath().getDestination();
        return dest.isPresent() && dest.get().equals(destination);
    }
}