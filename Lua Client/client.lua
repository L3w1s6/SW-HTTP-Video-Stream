W, H = 64, 64
debugStr = ""
httpPort = 8080
debug = true

-- table constructor using [key] = value
replyErrors = {
	["connect(): Connection refused"] = "no webserver running, slowed",
	["timeout"] = "connection timed out"
}

tick = 0
function onTick()
	tickRate = property.getNumber("Tick Interval")
	httpPort = property.getNumber("HTTP Port")
	
	if tick % tickRate == 0 then -- Don't spam every tick
		async.httpGet(httpPort, string.format("/stream?w=%d&h=%d", W, H)) -- request data (send data in URI)
	end
	tick = (tick + 1) % tickRate
end

-- return bool if string has the prefix
function hasPrefix(s, prefix)
	if #s > 0 then
		i, _ = string.find(s, prefix)
		return i == 1
	end
	return false
end

-- check for errors (no header so SW puts in res body)
function hasReplyErrors(resBody)
	if replyErrors[resBody] ~= nil then
		debugStr = replyErrors[resBody]
		return true
	else
		return false
	end
end

-- Callback when Java responds
function httpReply(port, request_body, response_body)
	if debug then debugStr = string.format("%d|%s|%s", port, request_body, #response_body) end --for debug print
	
	if hasReplyErrors(response_body) then return end
	
	-- only set data if /stream reply, not /data
	if port == httpPort and hasPrefix(request_body, "/stream") then
	    frameData = response_body -- get received data
	end
end

function onDraw()
	if not frameData then return end -- Skip draw if no frame data
	
	W, H = screen.getWidth(), screen.getHeight()
	
	-- Iterate through each "R,G,B,X,Y,W|" group
    for r, g, b, x, y, w in string.gmatch(frameData, "(%d+),(%d+),(%d+),(%d+),(%d+),(%d+)|") do
        screen.setColor(tonumber(r), tonumber(g), tonumber(b))
        screen.drawRectF(tonumber(x), tonumber(y), tonumber(w), 1)
    end

	-- debug info (bottom left)
	if debug then
		screen.setColor(0, 0, 0)
		screen.drawRectF(0, H - 5, 109, 5)
		screen.setColor(255, 255, 255)
		screen.drawText(0, H - 5, debugStr)
	end
end