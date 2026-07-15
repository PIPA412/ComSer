package com.zsc.module.activity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.activity.domain.ComActivitySignup;
import com.zsc.module.activity.mapper.ComActivitySignupMapper;
import com.zsc.module.activity.service.IComActivitySignupService;
import org.springframework.stereotype.Service;

@Service
public class ComActivitySignupServiceImpl extends ServiceImpl<ComActivitySignupMapper, ComActivitySignup> implements IComActivitySignupService {
}
