package com.family.myfamily.common;

public class Constants {

    public static final class Role {
        public static final String ADMIN = "ADMIN";
        public static final String MEMBER = "MEMBER";
    }

    public static final class Status {
        public static final Integer ENABLED = 1;
        public static final Integer DISABLED = 0;
    }

    public static final class Gender {
        public static final String MALE = "MALE";
        public static final String FEMALE = "FEMALE";
    }

    public static final class PostType {
        public static final String EVENT = "EVENT";
        public static final String ACHIEVEMENT = "ACHIEVEMENT";
        public static final String MILESTONE = "MILESTONE";
        public static final String OTHER = "OTHER";
    }

    public static final class PostStatus {
        public static final String PENDING = "PENDING";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
    }
}
