package com.auth.module.security.contract.api.granttable;

import lombok.Getter;
import lombok.Setter;

/**
 * 授权主体
 *
 * @author Bunny
 */
@Getter
@Setter
public class GrantSubjectOption {

    private String subject;

    private String description;

}