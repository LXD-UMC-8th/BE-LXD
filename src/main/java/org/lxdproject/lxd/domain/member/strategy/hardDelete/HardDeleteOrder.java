package org.lxdproject.lxd.domain.member.strategy.hardDelete;

/**
 * 각 Strategy의 순서를 정의한다.
 */
public class HardDeleteOrder {

    // 인스턴스화 방지
    private HardDeleteOrder() {}

    public static final int DIARY_COMMENT_LIKE = 1;
    public static final int DIARY_LIKE = 2;
    public static final int DIARY_COMMENT = 3;
    public static final int FRIEND_REQUEST = 4;
    public static final int FRIEND = 5;
    public static final int NOTIFICATION = 6;

    // 멤버 삭제는 항상 마지막에 이뤄져야 하므로 99 값 할당
    public static final int MEMBER = 99;

}
