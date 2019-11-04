package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.lora.LoraWanPacket;
import IotDomain.networkentity.Mote;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;

import java.util.LinkedList;
import java.util.List;

public class ReplacePath implements ConsumePacketStrategy {

    private final static int BYTES_FOR_COORDINATE = Float.BYTES;
    private final static int BYTES_FOR_GEO_COORDINATE = BYTES_FOR_COORDINATE * 2;

    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        mote.setPath(extractPath(packet));
    }

    protected List<GeoPosition> extractPath(LoraWanPacket packet) {
        if ((packet.getPayload().length % BYTES_FOR_GEO_COORDINATE) != 0) {
            throw  new IllegalStateException("the packet doesn't contain the correct amount of byte");
        }
        var payload = Converter.toRowType(packet.getPayload());
        final List<GeoPosition> path = new LinkedList<>();
        for (int i = 0; i+ BYTES_FOR_GEO_COORDINATE <= payload.length; i+= BYTES_FOR_GEO_COORDINATE) {
            path.add(Converter.toGeoPosition(payload, i));
        }
        return path;
    }
}