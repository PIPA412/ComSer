package com.zsc.module.visitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.visitor.domain.ComVisitor;
import com.zsc.module.visitor.mapper.ComVisitorMapper;
import com.zsc.module.visitor.service.IComVisitorService;
import org.springframework.stereotype.Service;

@Service
public class ComVisitorServiceImpl extends ServiceImpl<ComVisitorMapper, ComVisitor> implements IComVisitorService {
}
