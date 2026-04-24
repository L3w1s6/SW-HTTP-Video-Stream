W, H = 64, 64
debugStr = ""
httpPort = 8080
serverRunning = true
debug = true

tick = 0
function onTick()
	tickRate = property.getNumber("Tick Interval")
	httpPort = property.getNumber("HTTP Port")
	
	if not serverRunning then tickRate = 60 end -- slow down if no server running
	
	if tick % tickRate == 0 then -- Don't spam every tick
		-- send data
		async.httpGet(8080, string.format("/data?w=%d&h=%d", W, H))
		
		-- request data
		async.httpGet(8080, "/stream")
	end
	tick = (tick + 1) % tickRate
end

-- Callback when Java responds
function httpReply(port, request_body, response_body)
	if debug then debugStr = port .. "|" .. request_body end --for debug print
	
	if response_body == "connect(): Connection refused" then
		debugStr = "no webserver running, slowed"
		return
	end
	
	-- only set data if /stream reply, not /data
	if port == httpPort and request_body == "/stream" then
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