package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.dto.TransactionViewDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardCondition;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardConditionRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.MaskCard;
import com.example.bankcards.util.bankUtils.BankUtil;
import com.example.bankcards.util.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");

    private final CardRepository cardRepository;
    private final @Qualifier("cardEnterMapper") DtoMapper<Card, CardEnteredDto> mapper;
    private final BankUtil bankUtil;
    private final UserService userService;
    private final @Qualifier("cardToCardShowMapper") DtoMapper<CardToShowDto, Card> toShowMapper;
    private final CardConditionRepository cardConditionRepository;

    @Transactional
    public void deleteCard(Long cardId){
        APP_LOG.info("Deleting card with id: {}", cardId);
        cardRepository.deleteById(cardId);
        APP_LOG.info("Card deleted successfully");
    }

    public boolean isCardOwner(Long cardId, String uuid){
        APP_LOG.debug("Checking if user {} is owner of card {}", uuid, cardId);
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFound("Card not found"))
                .getUser().getUserId().equals(UUID.fromString(uuid));
    }

    @Transactional
    public void addCard(CardEnteredDto cardEntered, UUID userId){
        APP_LOG.info("Adding new card for user: {}", userId);
        Card card = mapper.map(cardEntered);
        bankUtil.checkCardCondition(card);

        if (!userService.userExist(userId)){
            APP_LOG.error("User not found: {}", userId);
            throw new NotFound("User not found");
        }
        APP_LOG.info("Processing card addition");
        card.setUser(userService.findUserById(userId));
        card.setCardNumber(MaskCard.makeMaskOfCardNumber(card.getCardNumber()));
        bankUtil.makeBankToken(card);
        cardRepository.save(card);
        bankUtil.checkCardBalance(card);
        bankUtil.checkCardCondition(card);
        APP_LOG.info("Card added successfully for user: {}", userId);
    }

    public Page<CardToShowDto> showCards(UUID userId, Integer pageNumber, Integer pageSize){
        APP_LOG.debug("Showing cards from user: {}, page: {}, size: {}", userId, pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var page = cardRepository.findByUserId(userId, pageable);
        page.forEach(bankUtil::checkCardCondition);
        page.forEach(card -> {
            var condition = (cardConditionRepository.findLatestByCardId(card.getCardId()).orElseThrow());
            APP_LOG.debug(condition.getConditionName().getCode().toString());
            card.setResolvedCondition(condition);
        });
        page.forEach(bankUtil::checkCardBalance);
        return page.map(toShowMapper::map);
    }

    public Page<Card> showCards(Integer pageNumber, Integer pageSize){
        APP_LOG.debug("Showing all cards, page: {}, size: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var page = cardRepository.findAll(pageable);
        page.forEach(bankUtil::checkCardCondition);
        page.forEach(card -> {
            var condition = (cardConditionRepository.findLatestByCardId(card.getCardId()).orElseThrow());
            APP_LOG.debug(condition.getConditionName().getCode().toString());
            card.setResolvedCondition(condition);
        });
        page.forEach(bankUtil::checkCardBalance);
        return page;
    }

    public void blockCard(Long cardId){
        APP_LOG.info("Blocking card: {}", cardId);
        bankUtil.blockCard(cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFound("Card not found")));
        APP_LOG.info("Card blocked successfully");
    }

    public CardToShowDto getCard(Long cardId){
        Card card = bankUtil.checkCardBalance(
                bankUtil.checkCardCondition(
                        cardRepository.findById(cardId)
                                .orElseThrow(() -> new NotFound("Card not found"))));
        return toShowMapper.map(card);
    }

    public String getBankToken(Long cardId){
        return cardRepository.findById(cardId).orElseThrow().getBankToken();
    }
}