package fun.wswj.wrench.dcc.listener;

import fun.wswj.wrench.dcc.domain.model.valobj.AttributeVO;
import fun.wswj.wrench.dcc.domain.service.IDCCService;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCCAdjustListener implements MessageListener<AttributeVO> {
    private final Logger log = LoggerFactory.getLogger(DCCAdjustListener.class);

    private final IDCCService dccService;

    public DCCAdjustListener(IDCCService dccService) {
        this.dccService = dccService;
    }


    @Override
    public void onMessage(CharSequence charSequence, AttributeVO attributeVO) {
        try {
            log.info("sws-wrench dcc config attribute:{} value:{}", attributeVO.getAttribute(), attributeVO.getValue());
            dccService.updateConfig(attributeVO);
        } catch (Exception e) {
            log.error("sws-wrench dcc config attribute:{} value:{}", attributeVO.getAttribute(), attributeVO.getValue(), e);
        }

    }
}
