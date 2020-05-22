package seu.hy.killmall.service;

public interface RabbitmqSenderService {
    public void senderKillSuccessEmail(String orderNo);
    public void senderKillSuccessOrderExpireMsg(String orderNo);
}
