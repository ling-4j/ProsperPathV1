package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CategoryTestSamples.*;
import static com.mycompany.myapp.domain.TransactionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Transaction.class);
        Transaction transaction1 = getTransactionSample1();
        Transaction transaction2 = new Transaction();
        assertThat(transaction1).isNotEqualTo(transaction2);

        transaction2.setId(transaction1.getId());
        assertThat(transaction1).isEqualTo(transaction2);

        transaction2 = getTransactionSample2();
        assertThat(transaction1).isNotEqualTo(transaction2);
    }

    @Test
    void categoryTest() {
        Transaction transaction = getTransactionRandomSampleGenerator();
        Category categoryBack = getCategoryRandomSampleGenerator();

        transaction.setCategory(categoryBack);
        assertThat(transaction.getCategory()).isEqualTo(categoryBack);

        transaction.category(null);
        assertThat(transaction.getCategory()).isNull();
    }
}
