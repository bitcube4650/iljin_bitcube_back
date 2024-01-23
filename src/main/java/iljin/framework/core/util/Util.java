package iljin.framework.core.util;

import iljin.framework.core.security.user.User;
import iljin.framework.core.security.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Component
public class Util {

    final UserRepository userRepository;

    @Autowired
    public Util(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getLoginId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication().getPrincipal();

        String loginId = "";
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            loginId = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else {
            loginId = String.valueOf(principal);
        }
        return loginId;
    }

    public String getLoginUserId() {
        Optional<User> user = userRepository.findByLoginId(getLoginId());
        return user.map(User::getLoginId).orElse(null);
    }

    public String getLoginCompCd() {
        Optional<User> user = userRepository.findByLoginId(getLoginId());
        return user.map(User::getCompCd).orElse(null);
    }

    public User getLoginUser() {
        Optional<User> user = userRepository.findByLoginId(getLoginId());

        return user.orElse(null);
    }

    public String getPrevYearMonth(String yearMonth) {
        yearMonth = yearMonth.replaceAll("-", "");

        if(yearMonth.length() != 6) {
            throw new RuntimeException("년월의 형태가 잘못되었습니다. (YYYY-MM 또는 YYYYMM 가 아닙니다.) \n value : " + yearMonth);
        }
        String prevYearMonth = "";

        /*
        * case  : 202103
        * index : 012345
        * */
        String year = yearMonth.substring(0, 4);
        int intYear = Integer.parseInt(year);
        String month = yearMonth.substring(4);
        int intMonth = Integer.parseInt(month);
        if(intMonth == 1) {
            //전년도 12월
            intYear -= 1;
            year = String.valueOf(intYear);
            month = "12";

            prevYearMonth = year + month;
        } else {
            intMonth -= 1;
            if(intMonth <10) {
                //
                prevYearMonth = year + "0" + String.valueOf(intMonth);
            } else {
                prevYearMonth = year + String.valueOf(intMonth);
            }
        }

        return prevYearMonth;
    }

    public String getNextYearMonth(String yearMonth) {
        yearMonth = yearMonth.replaceAll("-", "");

        if(yearMonth.length() != 6) {
            throw new RuntimeException("년월의 형태가 잘못되었습니다. (YYYY-MM 또는 YYYYMM 가 아닙니다.) \n value : " + yearMonth);
        }
        String nextYearMonth = "";

        /*
         * case  : 202103
         * index : 012345
         * */
        String year = yearMonth.substring(0, 4);
        int intYear = Integer.parseInt(year);
        String month = yearMonth.substring(4);
        int intMonth = Integer.parseInt(month);
        if(intMonth == 12) {
            //전년도 12월
            intYear += 1;
            year = String.valueOf(intYear);
            month = "01";

            nextYearMonth = year + month;
        } else {
            intMonth += 1;
            if(intMonth <10) {
                //
                nextYearMonth = year + "0" + String.valueOf(intMonth);
            } else {
                nextYearMonth = year + String.valueOf(intMonth);
            }
        }

        return nextYearMonth;
    }

    public String getStartDate(String yearMonth) {
        yearMonth = yearMonth.replaceAll("-", "");

        if(yearMonth.length() != 6) {
            throw new RuntimeException("년월의 형태가 잘못되었습니다. (YYYY-MM 또는 YYYYMM 가 아닙니다.) \n value : " + yearMonth);
        }
        String startDate = yearMonth + "01";

        return startDate;
    }

    public BigDecimal stringToBicDecimal(String s){
        if(s.trim().length()==0)s="0";

        return BigDecimal.valueOf(Double.parseDouble(s.trim()));
    }

    public String nextDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        Date d = null;
        try {
            d = sdf.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.setTime(d);
        c.add(Calendar.DATE,1);
        return sdf.format(c.getTime());
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager manager){

        return new TransactionTemplate(manager);
    }
}
