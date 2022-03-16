using import Array
using import Option
using import glm
using import struct
using import String

import ..bottle
using bottle.gpu.types

let shader =
    String
        """"struct VertexOutput {
                [[location(0)]] vcolor: vec4<f32>;
                [[builtin(position)]] position: vec4<f32>;
            };

            var<private> vertices : array<vec3<f32>, 3u> = array<vec3<f32>, 3u>(
                vec3<f32>(0.0, 0.5, 0.0),
                vec3<f32>(-0.5, -0.5, 0.0),
                vec3<f32>(0.5, -0.5, 0.0),
            );

            var<private> colors : array<vec4<f32>, 3u> = array<vec4<f32>, 3u>(
                vec4<f32>(1.0, 0.0, 0.0, 1.0),
                vec4<f32>(0.0, 1.0, 0.0, 1.0),
                vec4<f32>(0.0, 0.0, 1.0, 1.0),
            );

            [[stage(vertex)]]
            fn vs_main([[builtin(vertex_index)]] vindex: u32) -> VertexOutput {
                var out: VertexOutput;
                out.position = vec4<f32>(vertices[vindex], 1.0);
                out.vcolor = colors[vindex];
                return out;
            }

            [[stage(fragment)]]
            fn fs_main([[location(0)]] vcolor: vec4<f32>) -> [[location(0)]] vec4<f32> {
                return vcolor;
            }

run-stage;

global pipeline : (Option GPUPipeline)

@@ 'on bottle.load
fn ()
    # TODO: maybe we'd prefer the shader type to be an enum?
    pipeline = (GPUPipeline "Empty" (GPUShaderModule shader 'wgsl))

@@ 'on bottle.draw
fn (render-pass)
    let pipeline = ('force-unwrap pipeline)

    'set-pipeline render-pass pipeline

    'draw render-pass 3:u32 1:u32 0:u32 0:u32

bottle.run;
