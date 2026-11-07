#!/bin/env fish

set start_timeout 60

set container_name "mc"
set container_engine "docker"

function fail
    set reason $argv[1]

    set_color red
    echo
    echo "FAILURE: $reason"
    set_color normal
    exit 1
end

set bq_jar $argv[1]

if not test -f "$bq_jar"
    fail "Cannot find BeautyQuests JAR file: $bq_jar"
end

set container_image "docker.io/itzg/minecraft-server"

echo "BeautyQuests JAR file: $bq_jar"

function test_ver
    echo

    set mc_version $argv[1]
    echo "Version: $mc_version"

    set server_type $argv[2]
    echo "Server type: $server_type"

    set container_image_version $argv[3]

    set -l extra_args $argv[4..]

    $container_engine pull -q "$container_image:$container_image_version"

    $container_engine run -d \
        --user 1000:1000 \
        -e EULA=TRUE \
        -e TYPE=$server_type \
        -e VERSION=$mc_version \
        -e ALLOW_NETHER=false \
        -e LEVEL_TYPE=flat \
        -v mc_version:/data/versions:Z \
        -v mc_cache:/data/cache:Z \
        -v mc_libraries:/data/libraries:Z \
        -v "$(realpath $bq_jar):/plugins/bq.jar:ro,Z" \
        --name "$container_name" \
        $extra_args \
        "$container_image:$container_image_version" > /dev/null

    echo Waiting for server to be running...
    while not test ($container_engine inspect -f {{.State.Health.Status}} "$container_name" 2>&1) = 'healthy'
        if not test ($container_engine inspect -f {{.State.Running}} "$container_name") = 'true'
            fail "Server failed to start"
        end
        if test ($container_engine inspect -f {{.State.Health.Status}} "$container_name" 2>&1) = ''
            # Known bug: https://github.com/podman-container-tools/podman/issues/18904
            echo "Healthcheck not working; waiting for $start_timeout seconds."
            sleep "$start_timeout"
            break
        end
        sleep 1
    end
    echo Server running!

    set debug_info ($container_engine exec "$container_name" rcon-cli beautyquests debugInfo)
    set -e debug_info[-1] # rcon-cli adds a weird last line

    if not begin; echo "$debug_info" | jq &> /dev/null; end
        # If the debugInfo command returned invalid JSON, we can assume it is
        # an "unknown command" error which means the plugin did not load
        # correctly.
        fail "Plugin has not loaded!"
    end

    set dbg_plugin_version (echo $debug_info | jq -r '.plugin_version')
    set dbg_server_version (echo $debug_info | jq -r '.server_version')
    set dbg_bukkit_version (echo $debug_info | jq -r '.bukkit_version')
    set dbg_paper_detected (echo $debug_info | jq -r '.paper_detected')

    if test "$dbg_server_version" != "$mc_version"
        fail "Wrong server version detected: $dbg_server_version"
    elif test ("$dbg_paper_detected" == true) != ("$server_type" == paper)
        fail "Wrong server type detected! Paper: $dbg_paper_detected"
    end
    echo "All good."

    $container_engine kill "$container_name" > /dev/null # no need to gracefully shutdown
    $container_engine rm "$container_name" &> /dev/null
end

# We reset the state at the beginning
$container_engine kill "$container_name" &> /dev/null
$container_engine rm "$container_name" &> /dev/null

for mc_version in "1.20.6" "1.21.4" "1.21.11"
    test_ver $mc_version paper java21
end

for mc_version in "26.1.2" "26.2" "26.3"
    test_ver $mc_version paper java25
end
