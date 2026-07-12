package com.zsc.module.convenience.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.convenience.domain.ComServiceItem;
import com.zsc.module.convenience.mapper.ComServiceItemMapper;
import com.zsc.module.convenience.service.IComServiceItemService;
import org.springframework.stereotype.Service;

@Service
public class ComServiceItemServiceImpl extends ServiceImpl<ComServiceItemMapper, ComServiceItem> implements IComServiceItemService {
}
