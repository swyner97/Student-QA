package application;

import org.junit.jupiter.api.Test;

import model.Question;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {

    @Test
    void getStatusText_returnsUnresolved_byDefault() {
        Question q = new Question(1, 10, "Alice", "Title", "Desc");
        assertEquals("Unresolved", q.getStatusText());
    }

    @Test
    void markResolved_changesStatus() {
        Question q = new Question(1, 10, "Alice", "Title", "Desc");
        q.markResolved();
        assertEquals("Resolved", q.getStatusText());
    }

    @Test
    void followUp_defaultIsZero() {
        Question q = new Question(1, 10, "Alice", "Title", "Desc");
        assertEquals(0, q.getFollowUp());
    }
}