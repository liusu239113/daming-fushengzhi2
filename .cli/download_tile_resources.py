#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
download_tile_resources.py - 地形 pak 资源下载工具

用法:
    python3 /workspace/.cli/download_tile_resources.py

说明:
    - 从 official-res CDN 下载地形所需的 pak 资源到 /workspace/.cli/terrain-res/
    - 幂等：文件已存在则跳过下载
    - 环境变量 TAPTAP_MCP_ENV=production 时使用 stable 版本，否则使用 latest
"""

import sys
import os
import json
import urllib.request
import urllib.error
from pathlib import Path


CDN_BASE = 'https://tapcode-sce.spark.xd.com/src/official-res'

# 地形所需的 pak 资源 UUID 列表
TILE_RESOURCE_UUIDS = [
    'C2r6mQjJeFAPmPFfIvmdrci4',
]

# 下载目标目录（脚本同目录的 terrain-res/）
SCRIPT_DIR = Path(__file__).parent
TERRAIN_RES_DIR = SCRIPT_DIR / 'terrain-res'


def get_version_url() -> str:
    env = os.environ.get('TAPTAP_MCP_ENV', 'rnd')
    filename = 'stable.json' if env == 'production' else 'latest.json'
    return f'{CDN_BASE}/{filename}'


def fetch_json(url: str, timeout: int = 30) -> dict:
    req = urllib.request.Request(url, headers={'User-Agent': 'UrhoX-Builder/1.0'})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode('utf-8'))


def fetch_version_info() -> dict:
    url = get_version_url()
    print(f'  获取版本信息: {url}')
    return fetch_json(url)


def fetch_manifest(version: str, client: str) -> dict:
    cache_path = TERRAIN_RES_DIR / f'manifest-{client}.json'
    if cache_path.exists():
        print(f'  使用缓存 manifest: {cache_path.name}')
        with open(cache_path, 'r', encoding='utf-8') as f:
            return json.load(f)

    url = f'{CDN_BASE}/{version}/manifest-{client}.json'
    print(f'  下载 manifest: {url}')
    data = fetch_json(url)

    cache_path.parent.mkdir(parents=True, exist_ok=True)
    with open(cache_path, 'w', encoding='utf-8') as f:
        json.dump(data, f)
    return data


def find_entry(files: list, uuid: str) -> dict | None:
    for entry in files:
        if entry.get('uuid') == uuid:
            return entry
    return None


def download_resource(uuid: str, hash_: str, ext: str) -> Path:
    TERRAIN_RES_DIR.mkdir(parents=True, exist_ok=True)

    filename = f'{uuid}-{hash_}{ext}'
    target = TERRAIN_RES_DIR / filename

    if target.exists():
        print(f'  [已存在] {filename}，跳过下载')
        return target

    # 删除同 UUID 的旧版本文件（hash 已变化）
    for old in TERRAIN_RES_DIR.glob(f'{uuid}-*'):
        old.unlink()
        print(f'  [清理] 旧版本: {old.name}')

    url = f'{CDN_BASE}/assets/{filename}'
    print(f'  下载: {url}')

    req = urllib.request.Request(url, headers={'User-Agent': 'UrhoX-Builder/1.0'})
    with urllib.request.urlopen(req, timeout=120) as resp:
        data = resp.read()

    with open(target, 'wb') as f:
        f.write(data)

    print(f'  [完成] {filename} ({len(data) / 1024 / 1024:.1f} MB)')
    return target


def main():
    print(f'准备下载 {len(TILE_RESOURCE_UUIDS)} 个地形 pak 资源...')

    try:
        version_info = fetch_version_info()
        version = version_info['version']
        client = version_info['client']
    except Exception as e:
        print(f'错误: 获取版本信息失败: {e}', file=sys.stderr)
        sys.exit(1)

    try:
        manifest = fetch_manifest(version, client)
    except Exception as e:
        print(f'错误: 获取 manifest 失败: {e}', file=sys.stderr)
        sys.exit(1)

    files = manifest.get('files', [])

    success = 0
    failed = []
    for uuid in TILE_RESOURCE_UUIDS:
        entry = find_entry(files, uuid)
        if not entry:
            print(f'  [未找到] UUID 不在 manifest 中: {uuid}', file=sys.stderr)
            failed.append(uuid)
            continue
        try:
            download_resource(uuid, entry['hash'], entry['ext'])
            success += 1
        except Exception as e:
            print(f'  [失败] {uuid}: {e}', file=sys.stderr)
            failed.append(uuid)

    print(f'\n完成: {success} 成功, {len(failed)} 失败')
    if failed:
        print(f'失败的 UUID: {", ".join(failed)}', file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
