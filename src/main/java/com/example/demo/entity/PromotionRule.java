package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Promotion_Rules")
@Getter
@Setter
public class PromotionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private RuleType ruleType;

    @Column(name = "rule_value", length = 500)
    private String ruleValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 10)
    private Operator operator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Enum cho loại điều kiện
    public enum RuleType {
        PRODUCT("Sản phẩm"),
        CATEGORY("Danh mục"),
        BRAND("Thương hiệu"),
        QUANTITY("Số lượng"),
        VALUE("Giá trị"),
        USER_GROUP("Nhóm người dùng");

        private final String displayName;

        RuleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum cho toán tử so sánh
    public enum Operator {
        EQUALS("Bằng"),
        GREATER_THAN("Lớn hơn"),
        LESS_THAN("Nhỏ hơn"),
        GREATER_THAN_OR_EQUAL("Lớn hơn hoặc bằng"),
        LESS_THAN_OR_EQUAL("Nhỏ hơn hoặc bằng"),
        IN("Trong danh sách"),
        NOT_IN("Không trong danh sách");

        private final String displayName;

        Operator(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

