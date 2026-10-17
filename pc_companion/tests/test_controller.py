from src.controller.mock_output import MockGamepad
from src.protocol import InputPacket

def test_mock_controller_tracking():
    mappings = {
        "handbrake_button": "A",
        "gear_up_button": "RIGHT_SHOULDER",
        "gear_down_button": "LEFT_SHOULDER"
    }
    gamepad = MockGamepad(mappings)
    assert gamepad.is_active() is True

    packet = InputPacket(
        steering=-0.5,
        throttle=0.75,
        brake=0.25,
        handbrake=True,
        gear_up=True
    )
    gamepad.apply_inputs(packet)
    assert gamepad.updates_count == 1
    assert gamepad.last_packet.steering == -0.5
    assert gamepad.last_packet.throttle == 0.75
    assert gamepad.last_packet.brake == 0.25
    assert gamepad.last_packet.handbrake is True

    status = gamepad.get_status()
    assert status["driver_ready"] is True
    assert status["is_mock"] is True
    assert status["updates_count"] == 1

    gamepad.close()
    assert gamepad.is_active() is False
