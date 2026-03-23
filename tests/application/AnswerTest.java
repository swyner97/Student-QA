package application;

import org.junit.jupiter.api.Test;

import model.Answer;

import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {

    @Test
    void toString_includesSolutionLabel() {
        Answer a = new Answer(1, 10, 5, "Bob", "Hello", "2026-03-23T10:00:00", true);
        assertTrue(a.toString().contains("SOLUTION"));
    }

    @Test
    void isSolution_defaultFalse() {
        Answer a = new Answer(1, 10, 5, "Bob", "Hello");
        assertFalse(a.isSolution());
    }

    @Test
    void setSolution_updatesValue() {
        Answer a = new Answer(1, 10, 5, "Bob", "Hello");
        a.setSolution(true);
        assertTrue(a.isSolution());
    }
}