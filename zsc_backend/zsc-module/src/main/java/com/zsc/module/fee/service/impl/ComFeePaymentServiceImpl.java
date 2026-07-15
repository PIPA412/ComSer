package com.zsc.module.fee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.fee.domain.ComFeePayment;
import com.zsc.module.fee.mapper.ComFeePaymentMapper;
import com.zsc.module.fee.service.IComFeePaymentService;
import org.springframework.stereotype.Service;

@Service
public class ComFeePaymentServiceImpl extends ServiceImpl<ComFeePaymentMapper, ComFeePayment> implements IComFeePaymentService {
}
