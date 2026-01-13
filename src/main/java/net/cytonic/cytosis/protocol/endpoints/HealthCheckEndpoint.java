package net.cytonic.cytosis.protocol.endpoints;

import com.google.errorprone.annotations.Keep;

import net.cytonic.cytosis.CytosisContext;
import net.cytonic.protocol.Endpoint;
import net.cytonic.protocol.NotifyData;
import net.cytonic.protocol.objects.HealthCheckProtocolObject;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Packet;
import net.cytonic.protocol.objects.HealthCheckProtocolObject.Response;

@Keep
public class HealthCheckEndpoint implements
    Endpoint<HealthCheckProtocolObject.Packet, HealthCheckProtocolObject.Response> {

    @Override
    public Response onMessage(Packet message, NotifyData extraData) {
        return new Response();
    }

    @Override
    public String getSubject() {
        return "health.check." + CytosisContext.SERVER_ID;
    }
}
