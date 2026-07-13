package com.zsc.module.fee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.fee.domain.ComFeeItem;
import com.zsc.module.fee.mapper.ComFeeItemMapper;
import com.zsc.module.fee.service.IComFeeItemService;
import org.springframework.stereotype.Service;

@Service
public class ComFeeItemServiceImpl extends ServiceImpl<ComFeeItemMapper, ComFeeItem> implements IComFeeItemService {
}
