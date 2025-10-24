package com.example.bankcards.service;

import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.MaskCard;
import com.example.bankcards.util.bankUtils.BankUtil;
import com.example.bankcards.util.mapper.CardToCardShowMapper;
import com.example.bankcards.util.mapper.DtoMapper;
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
                       @Qualifier("CardToCardShowMapper") DtoMapper toShowMapper) {
        this.cardRepository = cardRepository;
        this.mapper = mapper;
        this.bankUtil = bankUtil;
        this.userService = userService;
        this.toShowMapper = toShowMapper;
    }

    public void deleteCard(CardEnteredDto card){
        cardRepository.deleteByCardNumberAndExpireDate(
                MaskCard.makeMaskOfCardNumber(card.getCardNumber()),
                card.getExpirationDate());
    }

    public boolean isCardOwner(Long cardId, UUID userId){
        return cardRepository.findById(cardId).orElseThrow(
                () -> new NotFound("Card not found")
        ).getUser().getUserId().equals(userId);
    }

    @Transactional
    public void addCard(CardEnteredDto cardEntered, UUID userId){
        Card card = mapper.map(cardEntered);
        card.setCondition(bankUtil.checkCardCondition(card));
        if (!userService.userExist(userId)){
            throw new NotFound("User not found");
        }
        card.setUser(userService.findUserById(userId));
        card.setCardNumber(MaskCard.makeMaskOfCardNumber(card.getCardNumber()));
        card.setBalance(bankUtil.checkCardBalance(card));
        bankUtil.makeBankToken(card);
        cardRepository.save(card);
    }

    public Page<CardToShowDto> showCards(UUID userID, Integer pageNumber, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return cardRepository.findByUserId(userID, pageable)
                .map(toShowMapper::map);

    }

    public Page<Card> showCards(Integer pageNumber, Integer pageSize){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return cardRepository.findAll(pageable);
    }

    public void blockCard(CardEnteredDto cardEnteredDto){
        bankUtil.blockCard(findCard(cardEnteredDto).orElseThrow(
                        () -> new NotFound("Card not found")));
    }

    public void unblockCard(CardEnteredDto cardEnteredDto){
        bankUtil.unblockCard(findCard(cardEnteredDto).orElseThrow(
                () -> new NotFound("Card not found")));
    }

    private Optional<Card> findCard(CardEnteredDto cardEnteredDto){
        return cardRepository.findByCardNumberAndExpireDate(
                MaskCard.makeMaskOfCardNumber(cardEnteredDto.getCardNumber()),
                cardEnteredDto.getExpirationDate());
    }
}
