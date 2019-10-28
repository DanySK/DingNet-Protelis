package IotDomain;

import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;

import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Map;

public class UserMote extends Mote {

    private boolean isActive = false;
    private final GeoPosition destination = new GeoPosition(1,1);
    private final LocalTime whenAskPath = LocalTime.of(0, 15);
    private boolean alreadyRequested = false;

    public UserMote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path, Double movementSpeed, Integer startMovementOffset, int periodSendingPacket, int startSendingOffset) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset);
    }

    public UserMote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path, Integer samplingRate, Double movementSpeed) {
        super(DevEUI, xPos, yPos, environment, transmissionPower, SF, moteSensors, energyLevel, path, samplingRate, movementSpeed);
    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        if (isActive && !alreadyRequested && whenAskPath.isBefore(getEnvironment().getClock().getTime())) {
            alreadyRequested = true;
            byte[] payload= new byte[17];
            payload[0] = 1;
            var lat = MapHelper.getInstance().toLatitude(getYPos());
            var lon = MapHelper.getInstance().toLongitude(getXPos());
            ByteBuffer.wrap(payload, 1, 4).putFloat((float)lat);
            ByteBuffer.wrap(payload, 5, 4).putFloat((float)lon);
            ByteBuffer.wrap(payload, 9, 4).putFloat((float)destination.getLatitude());
            ByteBuffer.wrap(payload, 13, 4).putFloat((float)destination.getLongitude());
            return new LoraWanPacket(getEUI(), getApplicationEUI(), Converter.toObjectType(payload), new LinkedList<>(macCommands.keySet()));
        } else {
            return super.composePacket(data, macCommands);
        }
    }

    public void setActive(boolean active) {
        if (active) {
            getEnvironment().getMotes().stream()
                .filter(m -> m instanceof UserMote)
                .map(m -> (UserMote)m)
                .forEach(m -> m.setActive(false));
        }
        isActive = active;
    }
}
