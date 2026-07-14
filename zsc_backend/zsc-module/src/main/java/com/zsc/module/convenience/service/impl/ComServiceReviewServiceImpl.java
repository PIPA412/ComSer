package com.zsc.module.convenience.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.convenience.domain.ComServiceReview;
import com.zsc.module.convenience.mapper.ComServiceReviewMapper;
import com.zsc.module.convenience.service.IComServiceReviewService;
import org.springframework.stereotype.Service;

@Service
public class ComServiceReviewServiceImpl extends ServiceImpl<ComServiceReviewMapper, ComServiceReview> implements IComServiceReviewService {
}
