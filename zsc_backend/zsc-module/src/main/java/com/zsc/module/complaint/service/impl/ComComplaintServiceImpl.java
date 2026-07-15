package com.zsc.module.complaint.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.complaint.domain.ComComplaint;
import com.zsc.module.complaint.mapper.ComComplaintMapper;
import com.zsc.module.complaint.service.IComComplaintService;
import org.springframework.stereotype.Service;

@Service
public class ComComplaintServiceImpl extends ServiceImpl<ComComplaintMapper, ComComplaint> implements IComComplaintService {
}
