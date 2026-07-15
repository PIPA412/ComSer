package com.zsc.module.dashboard.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.dashboard.domain.ComAlertRule;
import com.zsc.module.dashboard.mapper.ComAlertRuleMapper;
import com.zsc.module.dashboard.service.IComAlertRuleService;
import org.springframework.stereotype.Service;

@Service
public class ComAlertRuleServiceImpl extends ServiceImpl<ComAlertRuleMapper, ComAlertRule> implements IComAlertRuleService {
}
