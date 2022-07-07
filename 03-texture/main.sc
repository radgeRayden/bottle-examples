using import String
using import Option
using import struct

import bottle
using bottle.gpu.types
import bottle.src.gpu.common

let stbi = (import stb.image)

from (import bottle.src.gpu.binding-interface) let GPUResourceBinding
from (import bottle.src.gpu.bindgroup) let GPUBindGroup

@@ 'on bottle.configure
fn (cfg)
    cfg.window.title = "bottle texture demo"

vvv bind shader
""""struct VertexOutput {
        @location(0) texcoords: vec2<f32>,
        @builtin(position) position: vec4<f32>,
    };

    var<private> vertices : array<vec3<f32>, 6u> = array<vec3<f32>, 6u>(
        vec3<f32>(-1.0,  1.0, 0.0), // tl
        vec3<f32>(-1.0, -1.0, 0.0), // bl
        vec3<f32>( 1.0, -1.0, 0.0), // br
        vec3<f32>( 1.0, -1.0, 0.0), // br
        vec3<f32>( 1.0,  1.0, 0.0), // tr
        vec3<f32>(-1.0,  1.0, 0.0), // tl
    );

    var<private> texcoords : array<vec2<f32>, 6u> = array<vec2<f32>, 6u>(
        vec2<f32>(0.0, 0.0),
        vec2<f32>(0.0, 1.0),
        vec2<f32>(1.0, 1.0),
        vec2<f32>(1.0, 1.0),
        vec2<f32>(1.0, 0.0),
        vec2<f32>(0.0, 0.0),
    );

    @group(0)
    @binding(1)
    var s : sampler;

    @group(0)
    @binding(2)
    var t : texture_2d<f32>;

    @vertex
    fn vs_main(@builtin(vertex_index) vindex: u32) -> VertexOutput {
        var out: VertexOutput;
        out.position = vec4<f32>(vertices[vindex], 1.0);
        out.texcoords = texcoords[vindex];
        return out;
    }

    @fragment
    fn fs_main(vertex: VertexOutput) -> @location(0) vec4<f32> {
        return textureSample(t, s, vertex.texcoords);
    }

fn load-image (filename)
    local w : i32
    local h : i32
    local channels : i32

    let data = (stbi.load filename &w &h &channels 4)
    print data w h channels
    assert (data != null)
    let texture = (GPUTexture data w h)
    stbi.image_free data

    texture

struct DrawState
    pipeline : GPUPipeline
    texture : GPUTexture
    bgroup : GPUBindGroup
    bgroup1 : GPUBindGroup

global draw-state : (Option DrawState)

@@ 'on bottle.load
fn ()
    let dummies = bottle.src.gpu.common.istate.dummy-resources
    let cache = bottle.src.gpu.common.istate.cached-layouts
    let layout0 =
        try
            'get cache.bind-group-layouts S"StreamingMesh"
        else
            error "you made a typo you dofus"
    let layout1 =
        try
            'get cache.bind-group-layouts S"Uniforms"
        else
            error "you made a typo you dofus"

    let linus = (load-image "linus.jpg")
    draw-state =
        DrawState
            pipeline = (GPUPipeline "Basic" (GPUShaderModule shader 'wgsl))
            bgroup =
                GPUBindGroup layout0
                    dummies.buffer
                    dummies.sampler
                    GPUResourceBinding.TextureView linus._view
            bgroup1 =
                GPUBindGroup layout1
                    dummies.uniform-buffer
            texture = linus

@@ 'on bottle.draw
fn (render-pass)
    let draw-state = ('force-unwrap draw-state)
    'set-pipeline render-pass draw-state.pipeline
    'set-bindgroup render-pass 0:u32 draw-state.bgroup
    'set-bindgroup render-pass 1:u32 draw-state.bgroup1

    'draw render-pass 6:u32 1:u32 0:u32 0:u32
    ;

bottle.run;
