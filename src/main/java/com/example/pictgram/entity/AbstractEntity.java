package com.example.pictgram.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

/**
 * 共通モデル
 * @see <a href="https://spring.pleiades.io/specifications/platform/8/apidocs/javax/persistence/mappedsuperclass">@MappedSuperclass</a>
 *
 */
@MappedSuperclass
@Data
public class AbstractEntity {
	
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * insert前に作成時間、更新時間を自動設定
     */
    @PrePersist
    public void onPrePersist() {
        Date date = new Date();
        setCreatedAt(date);
        setUpdatedAt(date);
    }

    /**
     * update前に更新時間を自動設定
     */
    @PreUpdate
    public void onPreUpdate() {
        setUpdatedAt(new Date());
    }
}