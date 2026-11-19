package dev.piloto.modules.notes.domain;

import dev.piloto.core.db.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "notes")
public class Note extends BaseEntity {

    public static final int TITLE_MAX_LENGTH = 120;
    public static final int BODY_MAX_LENGTH = 2000;

    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(name = "body", length = BODY_MAX_LENGTH)
    private String body;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    protected Note() {
    }

    public Note(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
