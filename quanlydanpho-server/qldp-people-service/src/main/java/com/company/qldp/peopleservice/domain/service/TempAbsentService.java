package com.company.qldp.peopleservice.domain.service;

import com.company.qldp.common.DateInterval;
import com.company.qldp.common.Event;
import com.company.qldp.common.util.RandomCodeGenerator;
import com.company.qldp.domain.*;
import com.company.qldp.elasticsearchservice.domain.repository.PeopleSearchRepository;
import com.company.qldp.householdservice.api.repository.FamilyMemberRepository;
import com.company.qldp.householdservice.api.repository.HouseholdHistoryRepository;
import com.company.qldp.peopleservice.domain.dto.TempAbsentDto;
import com.company.qldp.peopleservice.domain.exception.PersonNotFoundException;
import com.company.qldp.peopleservice.domain.exception.TempAbsentNotFoundException;
import com.company.qldp.peopleservice.domain.repository.IDCardRepository;
import com.company.qldp.peopleservice.domain.repository.PeopleRepository;
import com.company.qldp.peopleservice.domain.repository.TempAbsentRepository;
import com.company.qldp.common.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.company.qldp.peopleservice.domain.repository.specification.GenericSpecification.*;

@Service
public class TempAbsentService {
    
    private TempAbsentRepository tempAbsentRepository;
    private IDCardRepository idCardRepository;
    private PeopleRepository peopleRepository;
    private PeopleSearchRepository peopleSearchRepository;
    private FamilyMemberRepository familyMemberRepository;
    private HouseholdHistoryRepository householdHistoryRepository;
    
    @Autowired
    public TempAbsentService(
        TempAbsentRepository tempAbsentRepository,
        IDCardRepository idCardRepository,
        PeopleRepository peopleRepository,
        PeopleSearchRepository peopleSearchRepository,
        FamilyMemberRepository familyMemberRepository,
        HouseholdHistoryRepository householdHistoryRepository
    ) {
        this.tempAbsentRepository = tempAbsentRepository;
        this.idCardRepository = idCardRepository;
        this.peopleRepository = peopleRepository;
        this.peopleSearchRepository = peopleSearchRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.householdHistoryRepository = householdHistoryRepository;
    }
    
    @Transactional
    public TempAbsent createTempAbsent(TempAbsentDto tempAbsentDto) {
        People people = idCardRepository.findByIdCardNumber(tempAbsentDto.getIdCardNumber())
            .getPerson();
        
        if (people == null) {
            throw new PersonNotFoundException();
        }
        
        DateInterval interval = DateUtils.createDateInterval(
            tempAbsentDto.getFromDate(),
            tempAbsentDto.getToDate()
        );
        
        String code = RandomCodeGenerator.generateCode(8);
        while (tempAbsentCodeExists(code)) {
            code = RandomCodeGenerator.generateCode(8);
        }
        
        TempAbsent tempAbsent = TempAbsent.builder()
            .interval(interval)
            .person(people)
            .tempAbsentCode(code)
            .tempResidencePlace(tempAbsentDto.getTempResidentPlace())
            .reason(tempAbsentDto.getReason())
            .build();
        
        PersonalMobilization mobilization;
        
        if (people.getMobilization() == null) {
            mobilization = PersonalMobilization.builder()
                .leaveDate(tempAbsent.getInterval().getFrom())
                .leaveReason(tempAbsent.getReason())
                .newAddress(tempAbsent.getTempResidencePlace())
                .build();
        } else {
            mobilization = people.getMobilization();
            mobilization.setLeaveDate(tempAbsent.getInterval().getFrom());
            mobilization.setLeaveReason(tempAbsent.getReason());
            mobilization.setNewAddress(tempAbsent.getTempResidencePlace());
        }
        
        people.setMobilization(mobilization);
        
        People savedPeople = peopleRepository.save(people);
        
        peopleSearchRepository.findById(savedPeople.getId()).map(peopleSearch -> {
            peopleSearch.setLeaveDate(savedPeople.getMobilization().getLeaveDate());
            
            return peopleSearchRepository.save(peopleSearch);
        }).subscribe(Mono::subscribe);
    
        Household household = familyMemberRepository.findByPerson_Id(savedPeople.getId())
            .getHousehold();
        HouseholdHistory householdHistory = HouseholdHistory.builder()
            .household(household)
            .affectPerson(savedPeople)
            .date(savedPeople.getMobilization().getLeaveDate())
            .event(Event.TEMP_ABSENT)
            .build();
        householdHistoryRepository.save(householdHistory);
        
        return tempAbsentRepository.save(tempAbsent);
    }
    
    private boolean tempAbsentCodeExists(String code) {
        return tempAbsentRepository.findByTempAbsentCode(code) != null;
    }
    
    @Transactional
    public TempAbsent getTempAbsent(Integer id) {
        TempAbsent tempAbsent = tempAbsentRepository.findById(id)
            .orElseThrow(TempAbsentNotFoundException::new);
        getTempAbsentInfo(tempAbsent);
        
        return tempAbsent;
    }
    
    @Transactional
    public List<TempAbsent> getTempAbsentsByFilters(MultiValueMap<String, String> queryParams) {
        String dateRange = queryParams.getFirst("date");
    
        Specification<TempAbsent> spec = makeDateRangeSpecification(dateRange);
        
        List<TempAbsent> tempAbsents = tempAbsentRepository.findAll(spec);
        tempAbsents.forEach(this::getTempAbsentInfo);
        
        return tempAbsents;
    }
    
    private void getTempAbsentInfo(TempAbsent tempAbsent) {
        tempAbsent.getPerson().hashCode();
    }
}
