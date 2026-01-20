package com.dnikitin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the LibraryRunner application entry point.
 */
class LibraryRunnerTest {

    @Test
    void shouldReturnDefaultParamsWhenNoArgsProvided() {
        LibraryRunner.SimulationParams params = LibraryRunner.parseArguments(new String[]{});

        assertAll(
                () -> assertEquals(10, params.numReaders()),
                () -> assertEquals(3, params.numWriters()),
                () -> assertEquals(2000, params.restingTime())
        );
    }

    @Test
    void shouldParseTwoArgumentsCorrectly() {
        LibraryRunner.SimulationParams params = LibraryRunner.parseArguments(new String[]{"5", "2"});

        assertAll(
                () -> assertEquals(5, params.numReaders()),
                () -> assertEquals(2, params.numWriters()),
                () -> assertEquals(2000, params.restingTime()) // Default resting time
        );
    }

    @Test
    void shouldParseAllThreeArgumentsCorrectly() {
        LibraryRunner.SimulationParams params = LibraryRunner.parseArguments(new String[]{"8", "4", "500"});

        assertAll(
                () -> assertEquals(8, params.numReaders()),
                () -> assertEquals(4, params.numWriters()),
                () -> assertEquals(500, params.restingTime())
        );
    }

    @Test
    void shouldReturnDefaultsAndHandleErrorOnInvalidNumericInput() {
        LibraryRunner.SimulationParams params = LibraryRunner.parseArguments(new String[]{"not_a_number", "2"});

        assertAll(
                () -> assertEquals(10, params.numReaders()),
                () -> assertEquals(3, params.numWriters()),
                () -> assertEquals(2000, params.restingTime())
        );
    }
}