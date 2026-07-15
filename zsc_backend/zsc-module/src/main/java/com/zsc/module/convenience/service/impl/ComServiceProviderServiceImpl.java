package com.zsc.module.convenience.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.convenience.domain.ComServiceProvider;
import com.zsc.module.convenience.mapper.ComServiceProviderMapper;
import com.zsc.module.convenience.service.IComServiceProviderService;
import org.springframework.stereotype.Service;

@Service
public class ComServiceProviderServiceImpl extends ServiceImpl<ComServiceProviderMapper, ComServiceProvider> implements IComServiceProviderService {
}
