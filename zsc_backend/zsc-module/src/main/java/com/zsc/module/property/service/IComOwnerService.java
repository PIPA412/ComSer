package com.zsc.module.property.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.property.domain.ComOwner;

public interface IComOwnerService extends IService<ComOwner> {

    /**
     * 根据手机号查询业主
     */
    ComOwner getByPhone(String phone);

    /**
     * 根据身份证号查询业主
     */
    ComOwner getByIdCard(String idCard);
}