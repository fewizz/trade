package ru.fewizz.trade;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallback;
import net.minecraft.world.timer.TimerCallbackSerializer;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class NotSerializableTimerCallback implements TimerCallback<MinecraftServer> {
	final TimerCallback<MinecraftServer> cb;
	
	public static class DummyTimerCallbackSerializer<T, C extends TimerCallback<T>>
	extends TimerCallback.Serializer<T, C> {

		public DummyTimerCallbackSerializer(Class<?> class_) {
			super(new Identifier("trade", "dummy"), class_);
		}

		@Override
		public void serialize(NbtCompound nbt, C callback) {
		}

		@Override
		public C deserialize(NbtCompound nbt) {
			return null;
		}
		
		static void register() {
			TimerCallbackSerializer.INSTANCE.registerSerializer(
				new DummyTimerCallbackSerializer(NotSerializableTimerCallback.class)
			);
		}
		
	}
	
	public NotSerializableTimerCallback(TimerCallback<MinecraftServer> cb) {
		this.cb = cb;
	}
	
	@Override
	public void call(MinecraftServer server, Timer<MinecraftServer> events, long time) {
		cb.call(server, events, time);
	}

}
