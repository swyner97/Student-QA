package model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import model.ContentType;

/**
 * Represents a staff-created moderation flag on a question or answer.
 */
public class ModerationFlag {
	private final int flagId;
	private final String itemType;
	private final int itemId;
	private final int staffId;
	private final String reason;
	private final String status;
	private final Timestamp createdAt;

	public ModerationFlag(int flagId, String itemType, int itemId, int staffId, String reason, String status, Timestamp createdAt) {
		this.flagId = flagId;
		this.itemType = itemType;
		this.itemId = itemId;
		this.staffId = staffId;
		this.reason = reason;
		this.status = status;
		this.createdAt = createdAt;
	}
	
	public int getFlagId() { return flagId; }
	public String getItemType() { return itemType; }
	public int getItemId() { return itemId; }
	public int getStaffId() { return staffId; }
	public String getReason() { return reason; }
	public String getStatus() { return status; }
	public Timestamp getCreatedAt() { return createdAt; }
	
	public ContentType getItemTypeEnum() {
		return ContentType.fromString(itemType);
	}
	
	public String getFormattedItemType() {
		return itemType.substring(0, 1).toUpperCase() + itemType.substring(1).toLowerCase();
	}
}

