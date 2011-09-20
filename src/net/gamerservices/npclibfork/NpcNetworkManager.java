package net.gamerservices.npclibfork;

import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketAddress;

import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public class NpcNetworkManager extends NetworkManager {

    public NpcNetworkManager(Socket paramSocket, String paramString, NetHandler paramNetHandler) {
        super(paramSocket, paramString, paramNetHandler);

        try {
            Field f = NetworkManager.class.getDeclaredField("l");
            f.setAccessible(true);
            f.set(this, false);
        } catch (Exception e) {

        }
    }

    @Override
    public void a(NetHandler paramNetHandler) {
        super.a(paramNetHandler);
    }

    @Override
    public void queue(Packet paramPacket) {
    }

    @Override
    public void a(String paramString, Object... paramArrayOfObject) {
    }

    @Override
    public void a() {
    }

    @Override
    public SocketAddress getSocketAddress() {
        return super.getSocketAddress();
    }

    @Override
    public void d() {
    }

    @Override
    public int e() {
        return 0;
    }
}
