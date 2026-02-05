package br.com.heimdex.dto;

import br.com.heimdex.model.enums.ItemStatus;

public class ResultadoChecklistResponseDTO {
    private Long itemTemplateId;
    private ItemStatus status;
    private String observacao;

    public ResultadoChecklistResponseDTO() {}

    public Long getItemTemplateId() { return itemTemplateId; }
    public void setItemTemplateId(Long itemTemplateId) { this.itemTemplateId = itemTemplateId; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}