package com.bsp.web;

import com.bsp.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 消息
 *
 * @author zksfromusa
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditRequest {
    private String editOptions;

    public static void main(String[] args) {
        int n = 5000;
        String lastPosId = "9999#0";
        for (int i = 0; i < n; ++i) {
            int user = (int) (Math.random() * 100);
            int cnt = i;
            String content = UUID.randomUUID().toString();
            String posId = user + "#" + cnt;
            String prevId = (int) (Math.random() * 100) + "#" + (int) (Math.random() * cnt);
            String nextId = (int) (Math.random() * 100) + "#" + (int) (Math.random() * cnt);
            if (cnt % 10 != 0) {
                System.out.println(String.format("insert(%s,%s,%s,%s)", posId, content, prevId, nextId));
            } else {
                System.out.println(String.format("del(%s)", posId));
            }

        }

    }
}
