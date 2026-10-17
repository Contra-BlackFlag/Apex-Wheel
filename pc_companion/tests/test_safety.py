import asyncio
import time
from src.protocol import InputPacket
from src.controller.mock_output import MockGamepad
from src.server import ControllerServer

def test_neutral_packet_safety():
    neutral = InputPacket.neutral()
    assert neutral.steering == 0.0
    assert neutral.throttle == 0.0
    assert neutral.brake == 0.0
    assert neutral.clutch == 0.0
    assert neutral.handbrake is False
    assert neutral.gear_up is False
    assert neutral.gear_down is False
    assert neutral.select is False
    assert neutral.start is False

def test_mock_gamepad_reset_all():
    gamepad = MockGamepad({"handbrake_button": "A"})
    # Apply held state
    held_packet = InputPacket(
        steering=0.8,
        throttle=1.0,
        brake=0.0,
        handbrake=True,
        gear_up=True
    )
    gamepad.apply_inputs(held_packet)
    assert gamepad.last_packet.throttle == 1.0
    assert gamepad.last_packet.handbrake is True

    # Call reset
    gamepad.reset_all()
    assert gamepad.last_packet.steering == 0.0
    assert gamepad.last_packet.throttle == 0.0
    assert gamepad.last_packet.handbrake is False
    assert gamepad.last_packet.gear_up is False
    assert gamepad.reset_count == 1

async def test_watchdog_timeout_triggers_neutral():
    gamepad = MockGamepad({})
    # Set 0.1s watchdog timeout for fast test
    server = ControllerServer(
        host="127.0.0.1",
        port=0,
        gamepad=gamepad,
        watchdog_timeout_seconds=0.10
    )

    # Simulate an active client connection with stuck throttle
    server.active_writer = object() # dummy active writer
    server.last_packet_time = time.time()
    gamepad.apply_inputs(InputPacket(throttle=1.0, steering=0.5, handbrake=True))
    assert gamepad.last_packet.throttle == 1.0

    # Start watchdog
    server.is_running = True
    watchdog_task = asyncio.create_task(server._watchdog_loop())

    # Wait 0.15s without updating last_packet_time
    await asyncio.sleep(0.15)

    # Watchdog should have fired reset_all()
    assert gamepad.last_packet.throttle == 0.0
    assert gamepad.last_packet.steering == 0.0
    assert gamepad.last_packet.handbrake is False
    assert gamepad.reset_count >= 1

    server.is_running = False
    watchdog_task.cancel()
