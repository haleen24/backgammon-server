package hse.adapter

import hse.adapter.dto.AnalyzeMatchRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping

@FeignClient("engine")
interface GameEngineAdapter {

    @PostMapping("game-engine/analyze")
    fun getAnalysis(request: AnalyzeMatchRequest): Map<Any, Any>
}