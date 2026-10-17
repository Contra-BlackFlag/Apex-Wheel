#!/usr/bin/env python3
"""
Standalone test runner using standard library unittest and asyncio.
Executes all test suites without external dependencies.
"""

import sys
import os
import unittest
import asyncio

# Add pc_companion to python path
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

from tests.test_protocol import (
    test_parse_valid_input_packet,
    test_clamping_out_of_bounds_inputs,
    test_version_mismatch_raises_error,
    test_malformed_json_raises_error,
    test_ping_and_pong
)
from tests.test_safety import (
    test_neutral_packet_safety,
    test_mock_gamepad_reset_all,
    test_watchdog_timeout_triggers_neutral
)
from tests.test_controller import test_mock_controller_tracking
from tests.test_server import test_server_socket_client_workflow

class TestCompanionSuite(unittest.TestCase):

    def test_protocol_valid(self):
        test_parse_valid_input_packet()

    def test_protocol_clamping(self):
        test_clamping_out_of_bounds_inputs()

    def test_protocol_version(self):
        test_version_mismatch_raises_error()

    def test_protocol_malformed(self):
        test_malformed_json_raises_error()

    def test_protocol_ping_pong(self):
        test_ping_and_pong()

    def test_safety_neutral(self):
        test_neutral_packet_safety()

    def test_safety_reset(self):
        test_mock_gamepad_reset_all()

    def test_safety_watchdog(self):
        asyncio.run(test_watchdog_timeout_triggers_neutral())

    def test_controller_mock(self):
        test_mock_controller_tracking()

    def test_server_client_workflow(self):
        asyncio.run(test_server_socket_client_workflow())

if __name__ == "__main__":
    unittest.main(verbosity=2)
