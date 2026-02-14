package model;

import java.sql.Timestamp;

/**
 * Represents an internal note attached to a moderation flag.
 */
public class ModerationNote {
	private final int noteId;
	private final int flagId;
	private final int staffId;
	private final String noteText;
	private final Timestamp createdAt;
	
	public ModerationNote(int noteId, int flagId, int staffId, String noteText, Timestamp createdAt) {
		this.noteId = noteId;
		this.flagId = flagId;
		this.staffId = staffId;
		this.noteText = noteText;
		this.createdAt = createdAt;
	}
	
	public int getNoteId() { return noteId; }
	public int getFlagId() { return flagId; }
	public int getStaffId() { return staffId; }
	public String getNoteText() { return noteText; }
	public Timestamp getCreatedAt() { return createdAt; }
}

