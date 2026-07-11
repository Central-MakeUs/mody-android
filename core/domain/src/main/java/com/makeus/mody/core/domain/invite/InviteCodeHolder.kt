package com.makeus.mody.core.domain.invite

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 딥링크(https://dev-mody.store/invite?code=XXX)로 들어온 초대 코드를 임시 보관.
 * MainActivity 가 set, 그룹 참여 화면(GroupViewModel)이 consume(1회성)하여 코드 필드에 채운다.
 *
 * process 생존 동안만 유효한 단순 보관소. 저장/영속 아님.
 */
@Singleton
class InviteCodeHolder @Inject constructor() {
    private var pending: String? = null

    fun set(code: String) {
        pending = code
    }

    /** 보관된 코드를 반환하고 비운다(1회성). 없으면 null. */
    fun consume(): String? = pending?.also { pending = null }
}
