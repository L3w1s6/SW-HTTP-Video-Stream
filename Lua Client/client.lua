W, H = 64, 64
debugStr = ""
httpPort = 8080
debug = true
slow = false

-- table constructor using [key] = value
replyErrors = {
	["connect(): Connection refused"] = "no webserver running, slowed",
	["timeout"] = "connection timed out",
	["Connection closed unexpectedly"] = "connection unexpectedly closed"
}

tick = 0
function onTick()
	tickRate = property.getNumber("Tick Interval")
	if slow then tickRate = tickRate * 10 end -- slow down if no server running
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
	if debug then debugStr = string.format("%d|%s|%s|%s", port, request_body, #response_body, response_body) end --for debug print
	
	if hasReplyErrors(response_body) then
		slow = true
		return
	else
		slow  = false
	end
	
	-- only set data if /stream reply, not /data
	if port == httpPort and hasPrefix(request_body, "/stream") then
	    frameData = response_body -- get received data
	end
end

-- combine all 8-bit segments into original int
function asciiToInt(s, start, stop)
	local len = stop - start
	local x = 0
	for i = 1, len do
		x = x + (string.byte(s, (start - 1) + i) << (8 * (len - i))) -- highest to lowest bits so shift up to original place before adding
	end
	return x
end

-- iterator for iterating over all sent data
function decode(s)
	local i = 1
	local len = #s
	return function()
		if len - i >= 9 then
			local i2 = i
			i = i + 9
			return asciiToInt(s, i2, i2 + 1), asciiToInt(s, i2 + 2, i2 + 3), asciiToInt(s, i2 + 4, i2 + 5), -- pos
				asciiToInt(s, i2 + 6, i2 + 6), asciiToInt(s, i2 + 7, i2 + 7), asciiToInt(s, i2 + 8, i2 + 8) -- colour
		else
			return nil
		end
	end
end

function onDraw()
	if not frameData then return end -- Skip draw if no frame data
	
	W, H = screen.getWidth(), screen.getHeight()
	
	-- iterate through each "XXYYWWRGB"
    for x, y, w, r, g, b in decode(frameData) do
        screen.setColor(r, g, b)
        screen.drawRectF(x, y, w, 1)
    end

	-- debug info (bottom left)
	if debug then
		screen.setColor(0, 0, 0)
		screen.drawRectF(0, H - 5, 109, 5)
		screen.setColor(255, 255, 255)
		screen.drawText(0, H - 5, debugStr)
		screen.drawText(0, H - 11, string.format("%d", tick))
	end
end