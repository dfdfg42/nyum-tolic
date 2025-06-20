package com.nyumtolic.nyumtolic.post.user;

import lombok.Getter;

@Getter
public enum BoardCategory {
    FREE("자유게시판", "free", "자유롭게 소통하는 공간입니다", "bi-chat-dots"),
    RESTAURANT_RECOMMEND("음식점 추천게시판", "restaurant", "맛집 추천과 리뷰를 공유해요", "bi-shop"),
    QNA("질문게시판", "qna", "궁금한 것들을 물어보세요", "bi-question-circle");

    private final String displayName;
    private final String urlPath;
    private final String description;
    private final String icon;

    BoardCategory(String displayName, String urlPath, String description, String icon) {
        this.displayName = displayName;
        this.urlPath = urlPath;
        this.description = description;
        this.icon = icon;
    }

    public static BoardCategory fromUrlPath(String urlPath) {
        for (BoardCategory category : values()) {
            if (category.urlPath.equals(urlPath)) {
                return category;
            }
        }
        return FREE; // 기본값
    }
}