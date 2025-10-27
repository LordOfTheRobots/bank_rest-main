package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.MaskCard;
import com.example.bankcards.util.bankUtils.BankUtil;
import com.example.bankcards.util.mapper.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CardService {
    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    @Autowired
    private final CardRepository cardRepository;

    @Autowired
    private final DtoMapper<Card, CardEnteredDto> mapper;

    @Autowired
    private final BankUtil bankUtil;

    @Autowired
    private final UserService userService;

    @Autowired
    private final DtoMapper<CardToShowDto, Card> toShowMapper;

    public CardService(CardRepository cardRepository,
                       @Qualifier("cardEnterMapper") DtoMapper mapper,
                       BankUtil bankUtil,
                       UserService userService,
                       @Qualifier("cardToCardShowMapper") DtoMapper toShowMapper) {
        this.cardRepository = cardRepository;
        this.mapper = mapper;
        this.bankUtil = bankUtil;
        this.userService = userService;
        this.toShowMapper = toShowMapper;
    }

    public void deleteCard(CardEnteredDto card){
        logger.info("Deleting card with number: {}", card.getCardNumber());
        cardRepository.deleteByCardNumberAndExpireDate(
                MaskCard.makeMaskOfCardNumber(card.getCardNumber()),
                card.getExpirationDate());
        logger.info("Card deleted successfully");
    }

    public boolean isCardOwner(Long cardId, UUID userId){
        logger.debug("Checking if user {} is owner of card {}", userId, cardId);
        return cardRepository.findById(cardId).orElseThrow(
                () -> new NotFound("Card not found")
        ).getUser().getUserId().equals(userId);
    }

    @Transactional
    public void addCard(CardEnteredDto cardEntered, UUID userId){
        logger.info("Adding new card for user: {}", userId);
        Card card = mapper.map(cardEntered);
        bankUtil.checkCardCondition(card);

        if (!userService.userExist(userId)){
            logger.error("User not found: {}", userId);
            throw new NotFound("User not found");
        }
        logger.info("Adding card {}", card);
        card.setUser(userService.findUserById(userId));
        card.setCardNumber(MaskCard.makeMaskOfCardNumber(card.getCardNumber()));
        bankUtil.checkCardBalance(card);
        bankUtil.makeBankToken(card);
        cardRepository.save(card);
        logger.info("Card added successfully for user: {}", userId);
    }

    public Page<CardToShowDto> showCards(UUID userID, Integer pageNumber, Integer pageSize){
        logger.debug("Showing cards for user: {}, page: {}, size: {}", userID, pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return cardRepository.findByUserUserId(userID, pageable)
                .map(toShowMapper::map);
    }

    public Page<Card> showCards(Integer pageNumber, Integer pageSize){
        logger.debug("Showing all cards, page: {}, size: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return cardRepository.findAll(pageable);
    }

    public void blockCard(CardEnteredDto cardEnteredDto){
        logger.info("Blocking card: {}", cardEnteredDto.getCardNumber());
        bankUtil.blockCard(findCard(cardEnteredDto).orElseThrow(
                () -> new NotFound("Card not found")));
        logger.info("Card blocked successfully");
    }

    public void unblockCard(CardEnteredDto cardEnteredDto){
        logger.info("Unblocking card: {}", cardEnteredDto.getCardNumber());
        bankUtil.unblockCard(findCard(cardEnteredDto).orElseThrow(
                () -> new NotFound("Card not found")));
        logger.info("Card unblocked successfully");
    }

    private Optional<Card> findCard(CardEnteredDto cardEnteredDto){
        logger.debug("Finding card by number and expiration date");
        return cardRepository.findByCardNumberAndExpireDate(
                MaskCard.makeMaskOfCardNumber(cardEnteredDto.getCardNumber()),
                cardEnteredDto.getExpirationDate());
    }
}