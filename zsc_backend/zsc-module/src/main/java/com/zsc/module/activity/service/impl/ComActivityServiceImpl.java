package com.zsc.module.activity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.activity.domain.ComActivity;
import com.zsc.module.activity.mapper.ComActivityMapper;
import com.zsc.module.activity.service.IComActivityService;
import org.springframework.stereotype.Service;

@Service
public class ComActivityServiceImpl extends ServiceImpl<ComActivityMapper, ComActivity> implements IComActivityService {
}
