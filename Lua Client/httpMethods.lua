-- Callback when Java responds
function httpReply(port, url, response_body)
    frameData = response_body -- Store the hex string
end

function onTick()
	if tick % 5 == 0 then -- Don't spam every tick
	    async.httpGet(8080, "/stream")
	end
end

function onDraw()
end