package com.zsc.module.complaint.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.complaint.domain.ComComplaintFeedback;
import com.zsc.module.complaint.mapper.ComComplaintFeedbackMapper;
import com.zsc.module.complaint.service.IComComplaintFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class ComComplaintFeedbackServiceImpl extends ServiceImpl<ComComplaintFeedbackMapper, ComComplaintFeedback> implements IComComplaintFeedbackService {
}
