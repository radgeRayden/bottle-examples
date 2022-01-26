using import Array
using import Option
using import glm
using import struct
using import String

import ..bottle
using bottle.gpu.types

let shader =
    String
        """"struct VertexAttributes {
                position : vec3<f32>;
                color : vec4<f32>;
            };

            struct Attributes {
                data : [[stride(32)]] array<VertexAttributes>;
            };

            [[group(0), binding(0)]] var<storage, read> attrs : Attributes;

            struct VertexOutput {
                [[location(0)]] vcolor: vec4<f32>;
                [[builtin(position)]] position: vec4<f32>;
            };

            [[stage(vertex)]]
            fn vs_main([[builtin(vertex_index)]] vindex: u32) -> VertexOutput {
                var out: VertexOutput;
                var vertex = attrs.data[vindex];
                out.vcolor = vertex.color;
                out.position = vec4<f32>(vertex.position, 1.0);
                return out;
            }

            [[stage(fragment)]]
            fn fs_main([[location(0)]] vcolor: vec4<f32>) -> [[location(0)]] vec4<f32> {
                return vcolor;
            }
run-stage;

global vertex-buffer : (Option GPUBuffer)
global pipeline : (Option GPUPipeline)

struct VertexAttributes plain
    position : vec3
    color : vec4

@@ 'on bottle.load
fn ()
    local vattributes : (Array VertexAttributes)
    local vertices =
        #   0
        #  /  \
        # 1----2
        arrayof vec3
            vec3  0.0  0.5 0.0
            vec3 -0.5 -0.5 0.0
            vec3  0.5 -0.5 0.0

    local colors =
        arrayof vec4
            vec4 1 0 0 1
            vec4 0 1 0 1
            vec4 0 0 1 1

    for i in (range 3)
        'append vattributes
            VertexAttributes (vertices @ i) (colors @ i)

    local pip =
        GPUPipeline (GPUShaderModule shader 'wgsl)

    local buf =
        GPUBuffer
            (sizeof ((typeof vattributes) . ElementType)) * (countof vattributes)
            'get-binding-layout pip
    'write buf vattributes

    pipeline = pip
    vertex-buffer = buf
    ;

@@ 'on bottle.draw
fn (render-pass)
    let buf = ('force-unwrap vertex-buffer)
    let pipeline = ('force-unwrap pipeline)

    'set-pipeline render-pass pipeline
    'bind-buffer render-pass buf
    'draw render-pass 3:u32 1:u32 0:u32 0:u32

bottle.run;
