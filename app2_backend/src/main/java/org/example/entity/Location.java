package org.example.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Id
    private String id;

    @Indexed(name = "location2dsphere")
    private Point location; // 使用 Spring Data MongoDB 的 Point 類

    private String userId;  // 關聯用戶 ID

    // 建構子方法，用於創建位置
    public void setLocation(double longitude, double latitude) {
        this.location = new Point(longitude, latitude);
    }
}