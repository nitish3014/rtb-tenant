package com.rtb.tenant.dto;


import java.util.List;

public record UpdateList(
    List<UpdateCommunicationChannelDetails> communications
) {
}
